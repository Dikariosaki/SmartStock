using System.Collections.Generic;
using System.Threading.Tasks;
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
    public class OrdenReabastecimientoProductosController : ControllerBase
    {
        private readonly IOrdenReabastecimientoService _service;

        public OrdenReabastecimientoProductosController(IOrdenReabastecimientoService service)
        {
            _service = service;
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet("orden/{ordenId:int}")]
        [SwaggerOperation(Summary = "Listar productos por orden de reabastecimiento")]
        public async Task<ActionResult<IEnumerable<OrdenReabastecimientoProductoResponse>>> GetByOrden(int ordenId)
        {
            var items = await _service.GetProductosByOrdenAsync(ordenId);
            return Ok(items);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet("{ordenId:int}/{productoId:int}")]
        [SwaggerOperation(Summary = "Obtener relación orden-producto por ids")]
        public async Task<ActionResult<OrdenReabastecimientoProductoResponse>> GetByIds(int ordenId, int productoId)
        {
            var item = await _service.GetProductoByIdsAsync(ordenId, productoId);
            if (item == null) return NotFound();
            return Ok(item);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPost]
        [SwaggerOperation(Summary = "Crear relación orden-producto")]
        public async Task<ActionResult<OrdenReabastecimientoProductoResponse>> Create([FromBody] OrdenReabastecimientoProductoCreateRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);

            var response = await _service.AddProductoAsync(request);
            return CreatedAtAction(nameof(GetByIds), new { ordenId = response.OrdenId, productoId = response.ProductoId }, response);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPut("{ordenId:int}/{productoId:int}")]
        [SwaggerOperation(Summary = "Actualizar relación orden-producto")]
        public async Task<IActionResult> Update(int ordenId, int productoId, [FromBody] OrdenReabastecimientoProductoUpdateRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);

            try
            {
                await _service.UpdateProductoAsync(ordenId, productoId, request);
                return NoContent();
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }

        [Authorize(Roles = "administrador")]
        [HttpDelete("{ordenId:int}/{productoId:int}")]
        [SwaggerOperation(Summary = "Eliminar relación orden-producto")]
        public async Task<IActionResult> Delete(int ordenId, int productoId)
        {
            try
            {
                await _service.RemoveProductoAsync(ordenId, productoId);
                return NoContent();
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPost("{ordenId:int}/{productoId:int}/activate")]
        [SwaggerOperation(Summary = "Activar producto en orden")]
        public async Task<IActionResult> Activate(int ordenId, int productoId)
        {
            try
            {
                await _service.ActivateProductoAsync(ordenId, productoId);
                return NoContent();
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPost("{ordenId:int}/{productoId:int}/deactivate")]
        [SwaggerOperation(Summary = "Desactivar producto en orden")]
        public async Task<IActionResult> Deactivate(int ordenId, int productoId)
        {
            try
            {
                await _service.DeactivateProductoAsync(ordenId, productoId);
                return NoContent();
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }
    }
}
