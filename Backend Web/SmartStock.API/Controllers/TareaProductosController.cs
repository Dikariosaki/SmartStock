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
    public class TareaProductosController : ControllerBase
    {
        private readonly ITareaService _service;

        public TareaProductosController(ITareaService service)
        {
            _service = service;
        }

        [Authorize(Roles = "administrador,supervisor,auxiliar")]
        [HttpGet("tarea/{tareaId:int}")]
        [SwaggerOperation(Summary = "Listar productos asociados a una tarea")]
        [ProducesResponseType(typeof(IEnumerable<TareaProductoResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<TareaProductoResponse>>> GetByTarea(int tareaId)
        {
            var items = await _service.GetProductosByTareaAsync(tareaId);
            return Ok(items);
        }

        [Authorize(Roles = "administrador,supervisor,auxiliar")]
        [HttpGet("producto/{productoId:int}")]
        [SwaggerOperation(Summary = "Listar tareas asociadas a un producto")]
        [ProducesResponseType(typeof(IEnumerable<TareaProductoResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<TareaProductoResponse>>> GetByProducto(int productoId)
        {
            var items = await _service.GetTareasByProductoAsync(productoId);
            return Ok(items);
        }

        [Authorize(Roles = "administrador,supervisor,auxiliar")]
        [HttpGet("{tareaId:int}/{productoId:int}")]
        [SwaggerOperation(Summary = "Obtener relación tarea-producto por ids")]
        [ProducesResponseType(typeof(TareaProductoResponse), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<TareaProductoResponse>> GetByIds(int tareaId, int productoId)
        {
            var item = await _service.GetProductoByIdsAsync(tareaId, productoId);
            if (item == null) return NotFound();
            return Ok(item);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPost]
        [SwaggerOperation(Summary = "Crear relación tarea-producto")]
        [ProducesResponseType(typeof(TareaProductoResponse), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<TareaProductoResponse>> Create([FromBody] TareaProductoCreateRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);

            try
            {
                var response = await _service.AddProductoAsync(request);
                return CreatedAtAction(nameof(GetByIds), new { tareaId = response.TareaId, productoId = response.ProductoId }, response);
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message });
            }
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPut("{tareaId:int}/{productoId:int}")]
        [SwaggerOperation(Summary = "Actualizar relación tarea-producto")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> Update(int tareaId, int productoId, [FromBody] TareaProductoUpdateRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);

            try
            {
                await _service.UpdateProductoAsync(tareaId, productoId, request);
                return NoContent();
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }

        [Authorize(Roles = "administrador")]
        [HttpDelete("{tareaId:int}/{productoId:int}")]
        [SwaggerOperation(Summary = "Eliminar relación tarea-producto")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> Delete(int tareaId, int productoId)
        {
            try
            {
                await _service.RemoveProductoAsync(tareaId, productoId);
                return NoContent();
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }
    }
}
