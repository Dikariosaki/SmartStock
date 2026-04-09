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
    public class TareasController : ControllerBase
    {
        private readonly ITareaService _service;

        public TareasController(ITareaService service)
        {
            _service = service;
        }

        [Authorize(Roles = "administrador,supervisor,auxiliar")]
        [HttpGet]
        [SwaggerOperation(Summary = "Listar tareas paginadas", Description = "Obtiene tareas con paginación y filtro de estado.")]
        [ProducesResponseType(typeof(PagedResponse<TareaResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<PagedResponse<TareaResponse>>> GetAll([FromQuery] PagedRequest request)
        {
            var items = await _service.GetPagedAsync(request);
            return Ok(items);
        }

        [Authorize(Roles = "administrador,supervisor,auxiliar")]
        [HttpGet("{id:int}")]
        [SwaggerOperation(Summary = "Obtener tarea por id")]
        [ProducesResponseType(typeof(TareaResponse), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<TareaResponse>> GetById(int id)
        {
            var item = await _service.GetByIdAsync(id);
            if (item == null) return NotFound();
            return Ok(item);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPost]
        [SwaggerOperation(Summary = "Crear tarea")]
        [ProducesResponseType(typeof(TareaResponse), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<TareaResponse>> Create([FromBody] TareaCreateRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);

            var response = await _service.CreateAsync(request);
            return CreatedAtAction(nameof(GetById), new { id = response.TareaId }, response);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPut("{id:int}")]
        [SwaggerOperation(Summary = "Actualizar tarea")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> Update(int id, [FromBody] TareaUpdateRequest request)
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
        [SwaggerOperation(Summary = "Eliminar tarea")]
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
        [SwaggerOperation(Summary = "Activar tarea")]
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
        [SwaggerOperation(Summary = "Desactivar tarea")]
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
