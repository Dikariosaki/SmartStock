using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;
using SmartStock.Application.Interfaces;
using Swashbuckle.AspNetCore.Annotations;

namespace SmartStock.API.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]")]
    public class MovimientosController : ControllerBase
    {
        private readonly IMovimientoService _service;

        public MovimientosController(IMovimientoService service)
        {
            _service = service;
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet]
        [SwaggerOperation(Summary = "Listar movimientos paginados", Description = "Obtiene movimientos con paginación y filtro de estado.")]
        [ProducesResponseType(typeof(PagedResponse<MovimientoResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<PagedResponse<MovimientoResponse>>> GetAll([FromQuery] PagedRequest request)
        {
            var movimientos = await _service.GetPagedAsync(request);
            return Ok(movimientos);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet("{id:int}")]
        [SwaggerOperation(Summary = "Obtener movimiento por id")]
        public async Task<ActionResult<MovimientoResponse>> GetById(int id)
        {
            var mov = await _service.GetByIdAsync(id);
            if (mov == null) return NotFound();
            return Ok(mov);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet("inventario/{inventarioId:int}")]
        [SwaggerOperation(Summary = "Listar movimientos por inventario")]
        public async Task<ActionResult<IEnumerable<MovimientoResponse>>> GetByInventario(int inventarioId)
        {
            var movimientos = await _service.GetByInventarioAsync(inventarioId);
            return Ok(movimientos);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPost]
        [SwaggerOperation(Summary = "Crear movimiento")]
        public async Task<ActionResult<MovimientoResponse>> Create([FromBody] MovimientoCreateRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);

            var response = await _service.CreateAsync(request);
            return CreatedAtAction(nameof(GetById), new { id = response.MovimientoId }, response);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPut("{id:int}")]
        [SwaggerOperation(Summary = "Actualizar movimiento")]
        public async Task<IActionResult> Update(int id, [FromBody] MovimientoUpdateRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);

            try
            {
                await _service.UpdateAsync(id, request);
                return NoContent();
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }

        [Authorize(Roles = "administrador")]
        [HttpDelete("{id:int}")]
        [SwaggerOperation(Summary = "Eliminar movimiento")]
        public async Task<IActionResult> Delete(int id)
        {
            try
            {
                await _service.DeleteAsync(id);
                return NoContent();
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPost("{id:int}/activate")]
        [SwaggerOperation(Summary = "Activar movimiento")]
        public async Task<IActionResult> Activate(int id)
        {
            try
            {
                await _service.ActivateAsync(id);
                return NoContent();
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPost("{id:int}/deactivate")]
        [SwaggerOperation(Summary = "Desactivar movimiento")]
        public async Task<IActionResult> Deactivate(int id)
        {
            try
            {
                await _service.DeactivateAsync(id);
                return NoContent();
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }
    }
}
