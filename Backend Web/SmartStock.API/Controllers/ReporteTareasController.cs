using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartStock.Application.Interfaces;
using SmartStock.Application.DTOs;
using Swashbuckle.AspNetCore.Annotations;

namespace SmartStock.API.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]")]
    public class ReporteTareasController : ControllerBase
    {
        private readonly IReporteService _service;

        public ReporteTareasController(IReporteService service)
        {
            _service = service;
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet("reporte/{reporteId:int}")]
        [SwaggerOperation(Summary = "Listar tareas asociadas a un reporte")]
        [ProducesResponseType(typeof(IEnumerable<ReporteTareaResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ReporteTareaResponse>>> GetByReporte(int reporteId)
        {
            var items = await _service.GetTareasByReporteAsync(reporteId);
            return Ok(items);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet("tarea/{tareaId:int}")]
        [SwaggerOperation(Summary = "Listar reportes asociados a una tarea")]
        [ProducesResponseType(typeof(IEnumerable<ReporteTareaResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ReporteTareaResponse>>> GetByTarea(int tareaId)
        {
            var items = await _service.GetReportesByTareaAsync(tareaId);
            return Ok(items);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPost]
        [SwaggerOperation(Summary = "Crear relación reporte-tarea")]
        [ProducesResponseType(typeof(ReporteTareaResponse), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<ReporteTareaResponse>> Create([FromBody] ReporteTareaCreateRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);

            try
            {
                var response = await _service.AddTareaAsync(request);
                return CreatedAtAction(nameof(GetByReporte), new { reporteId = response.ReporteId }, response);
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message });
            }
        }

        [Authorize(Roles = "administrador")]
        [HttpDelete("{reporteId:int}/{tareaId:int}")]
        [SwaggerOperation(Summary = "Eliminar relación reporte-tarea")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> Delete(int reporteId, int tareaId)
        {
            try
            {
                await _service.RemoveTareaAsync(reporteId, tareaId);
                return NoContent();
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }
    }
}
