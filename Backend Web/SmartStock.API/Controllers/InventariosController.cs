using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartStock.Application.Interfaces;
using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;
using Swashbuckle.AspNetCore.Annotations;

namespace SmartStock.API.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]")]
    public class InventariosController : ControllerBase
    {
        private readonly IInventarioService _service;

        public InventariosController(IInventarioService service)
        {
            _service = service;
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet]
        [SwaggerOperation(Summary = "Listar inventarios paginados", Description = "Obtiene inventarios con paginación y filtro de estado.")]
        [ProducesResponseType(typeof(PagedResponse<InventarioResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<PagedResponse<InventarioResponse>>> GetAll([FromQuery] PagedRequest request)
        {
            var response = await _service.GetPagedAsync(request);
            return Ok(response);
        }

        [Authorize(Roles = "administrador,supervisor,auxiliar")]
        [HttpGet("alertas/stock-bajo")]
        [SwaggerOperation(
            Summary = "Listar alertas de stock bajo",
            Description = "Obtiene inventarios activos cuyo stock estÃ¡ por debajo del punto mÃ­nimo."
        )]
        [ProducesResponseType(typeof(IEnumerable<InventarioStockBajoResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<InventarioStockBajoResponse>>> GetStockBajoMinimo([FromQuery] int limit = 50)
        {
            var response = await _service.GetStockBajoMinimoAsync(limit);
            return Ok(response);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet("{id:int}")]
        [SwaggerOperation(Summary = "Obtener inventario por id")]
        [ProducesResponseType(typeof(InventarioResponse), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<InventarioResponse>> GetById(int id)
        {
            var inventario = await _service.GetByIdAsync(id);
            if (inventario == null) return NotFound();
            return Ok(inventario);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet("producto/{productoId:int}")]
        [SwaggerOperation(Summary = "Listar inventarios por producto")]
        [ProducesResponseType(typeof(IEnumerable<InventarioResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<InventarioResponse>>> GetByProducto(int productoId)
        {
            var inventarios = await _service.GetByProductoAsync(productoId);
            return Ok(inventarios);
        }

        [Authorize(Roles = "administrador")]
        [HttpPost]
        [SwaggerOperation(Summary = "Crear inventario")]
        [ProducesResponseType(typeof(InventarioResponse), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status409Conflict)]
        public async Task<ActionResult<InventarioResponse>> Create([FromBody] InventarioCreateRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);

            try
            {
                var response = await _service.CreateAsync(request);
                return CreatedAtAction(nameof(GetById), new { id = response.InventarioId }, response);
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message });
            }
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPut("{id:int}")]
        [SwaggerOperation(Summary = "Actualizar inventario")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        [ProducesResponseType(StatusCodes.Status409Conflict)]
        public async Task<IActionResult> Update(int id, [FromBody] InventarioUpdateRequest request)
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
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message });
            }
        }

        [Authorize(Roles = "administrador")]
        [HttpDelete("{id:int}")]
        [SwaggerOperation(Summary = "Eliminar inventario")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
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

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPost("entrada")]
        [SwaggerOperation(Summary = "Registrar entrada de producto")]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<IActionResult> RegistrarEntrada([FromBody] MovimientoStockRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);
            try
            {
                var cantidad = await _service.RegistrarEntradaAsync(request);
                return Ok(new { message = "Entrada registrada correctamente", cantidad });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPost("salida")]
        [SwaggerOperation(Summary = "Registrar salida de producto")]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<IActionResult> RegistrarSalida([FromBody] MovimientoStockRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);
            try
            {
                var cantidad = await _service.RegistrarSalidaAsync(request);
                return Ok(new { message = "Salida registrada correctamente", cantidad });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}
