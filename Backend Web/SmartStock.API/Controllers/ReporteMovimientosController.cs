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
    public class ReporteMovimientosController : ControllerBase
    {
        private readonly IReporteService _service;

        public ReporteMovimientosController(IReporteService service)
        {
            _service = service;
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet("reporte/{reporteId:int}")]
        [SwaggerOperation(Summary = "Listar movimientos asociados a un reporte")]
        [ProducesResponseType(typeof(IEnumerable<ReporteMovimientoResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ReporteMovimientoResponse>>> GetByReporte(int reporteId)
        {
            var items = await _service.GetMovimientosByReporteAsync(reporteId);
            return Ok(items);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet("movimiento/{movimientoId:int}")]
        [SwaggerOperation(Summary = "Listar reportes asociados a un movimiento")]
        [ProducesResponseType(typeof(IEnumerable<ReporteMovimientoResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ReporteMovimientoResponse>>> GetByMovimiento(int movimientoId)
        {
            var items = await _service.GetReportesByMovimientoAsync(movimientoId);
            return Ok(items);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPost]
        [SwaggerOperation(Summary = "Crear relación reporte-movimiento")]
        [ProducesResponseType(typeof(ReporteMovimientoResponse), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<ReporteMovimientoResponse>> Create([FromBody] ReporteMovimientoCreateRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);

            try
            {
                var response = await _service.AddMovimientoAsync(request);
                return CreatedAtAction(nameof(GetByReporte), new { reporteId = response.ReporteId }, response);
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message });
            }
        }

        [Authorize(Roles = "administrador")]
        [HttpDelete("{reporteId:int}/{movimientoId:int}")]
        [SwaggerOperation(Summary = "Eliminar relación reporte-movimiento")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> Delete(int reporteId, int movimientoId)
        {
            try
            {
                await _service.RemoveMovimientoAsync(reporteId, movimientoId);
                return NoContent();
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }
    }
}
