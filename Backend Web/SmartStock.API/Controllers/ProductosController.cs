using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartStock.Application.Interfaces;
using SmartStock.Application.DTOs;
using Swashbuckle.AspNetCore.Annotations;

using SmartStock.Application.DTOs.Common;

namespace SmartStock.API.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]")]
    public class ProductosController : ControllerBase
    {
        private readonly IProductoService _service;

        public ProductosController(IProductoService service)
        {
            _service = service;
        }

        [Authorize(Roles = "administrador,supervisor,auxiliar")]
        [HttpGet]
        [SwaggerOperation(Summary = "Listar productos paginados", Description = "Obtiene productos con paginación y filtro de estado.")]
        [ProducesResponseType(typeof(PagedResponse<ProductoResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<PagedResponse<ProductoResponse>>> GetAll([FromQuery] PagedRequest request)
        {
            var response = await _service.GetPagedAsync(request);
            return Ok(response);
        }

        [Authorize(Roles = "administrador,supervisor,auxiliar")]
        [HttpGet("{id:int}")]
        [SwaggerOperation(Summary = "Obtener producto por id")]
        [ProducesResponseType(typeof(ProductoResponse), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<ProductoResponse>> GetById(int id)
        {
            var producto = await _service.GetByIdAsync(id);
            if (producto == null) return NotFound();
            return Ok(producto);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPost]
        [SwaggerOperation(Summary = "Crear producto")]
        [ProducesResponseType(typeof(ProductoResponse), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status409Conflict)]
        public async Task<ActionResult<ProductoResponse>> Create([FromBody] ProductoCreateRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);

            try
            {
                var response = await _service.CreateAsync(request);
                return CreatedAtAction(nameof(GetById), new { id = response.ProductoId }, response);
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message });
            }
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPut("{id:int}")]
        [SwaggerOperation(Summary = "Actualizar producto")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        [ProducesResponseType(StatusCodes.Status409Conflict)]
        public async Task<IActionResult> Update(int id, [FromBody] ProductoUpdateRequest request)
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

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPost("{id:int}/deactivate")]
        [SwaggerOperation(Summary = "Dar de baja producto")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
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

        [Authorize(Roles = "administrador")]
        [HttpDelete("{id:int}")]
        [SwaggerOperation(Summary = "Eliminar producto")]
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
        [SwaggerOperation(Summary = "Activar producto")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
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
    }
}
