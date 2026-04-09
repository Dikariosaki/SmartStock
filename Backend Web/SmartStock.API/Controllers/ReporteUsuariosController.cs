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
    public class ReporteUsuariosController : ControllerBase
    {
        private readonly IReporteService _service;

        public ReporteUsuariosController(IReporteService service)
        {
            _service = service;
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet("reporte/{reporteId:int}")]
        [SwaggerOperation(Summary = "Listar usuarios asociados a un reporte")]
        [ProducesResponseType(typeof(IEnumerable<ReporteUsuarioResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ReporteUsuarioResponse>>> GetByReporte(int reporteId)
        {
            var items = await _service.GetUsuariosByReporteAsync(reporteId);
            return Ok(items);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet("usuario/{usuarioId:int}")]
        [SwaggerOperation(Summary = "Listar reportes asociados a un usuario")]
        [ProducesResponseType(typeof(IEnumerable<ReporteUsuarioResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ReporteUsuarioResponse>>> GetByUsuario(int usuarioId)
        {
            var items = await _service.GetReportesByUsuarioAsync(usuarioId);
            return Ok(items);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPost]
        [SwaggerOperation(Summary = "Crear relación reporte-usuario")]
        [ProducesResponseType(typeof(ReporteUsuarioResponse), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<ReporteUsuarioResponse>> Create([FromBody] ReporteUsuarioCreateRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);

            try
            {
                var response = await _service.AddUsuarioAsync(request);
                return CreatedAtAction(nameof(GetByReporte), new { reporteId = response.ReporteId }, response);
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message });
            }
        }

        [Authorize(Roles = "administrador")]
        [HttpDelete("{reporteId:int}/{usuarioId:int}")]
        [SwaggerOperation(Summary = "Eliminar relación reporte-usuario")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> Delete(int reporteId, int usuarioId)
        {
            try
            {
                await _service.RemoveUsuarioAsync(reporteId, usuarioId);
                return NoContent();
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }
    }
}
