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
    public class SubcategoriasController : ControllerBase
    {
        private readonly ISubcategoriaService _service;

        public SubcategoriasController(ISubcategoriaService service)
        {
            _service = service;
        }

        [Authorize(Roles = "administrador,supervisor,auxiliar")]
        [HttpGet]
        [SwaggerOperation(Summary = "Listar subcategorías paginadas", Description = "Obtiene subcategorías con paginación y filtro de estado.")]
        [ProducesResponseType(typeof(PagedResponse<SubcategoriaResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<PagedResponse<SubcategoriaResponse>>> GetAll([FromQuery] PagedRequest request)
        {
            var response = await _service.GetPagedAsync(request);
            return Ok(response);
        }

        [Authorize(Roles = "administrador,supervisor,auxiliar")]
        [HttpGet("{id:int}")]
        [SwaggerOperation(Summary = "Obtener subcategoría por id")]
        [ProducesResponseType(typeof(SubcategoriaResponse), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<SubcategoriaResponse>> GetById(int id)
        {
            var sub = await _service.GetByIdAsync(id);
            if (sub == null) return NotFound();
            return Ok(sub);
        }

        [Authorize(Roles = "administrador,supervisor,auxiliar")]
        [HttpGet("categoria/{categoriaId:int}")]
        [SwaggerOperation(Summary = "Listar subcategorías por categoría")]
        [ProducesResponseType(typeof(IEnumerable<SubcategoriaResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<SubcategoriaResponse>>> GetByCategoria(int categoriaId)
        {
            var subs = await _service.GetByCategoriaIdAsync(categoriaId);
            return Ok(subs);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPost]
        [SwaggerOperation(Summary = "Crear subcategoría")]
        [ProducesResponseType(typeof(SubcategoriaResponse), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status409Conflict)]
        public async Task<ActionResult<SubcategoriaResponse>> Create([FromBody] SubcategoriaCreateRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);

            try
            {
                var sub = await _service.CreateAsync(request);
                return CreatedAtAction(nameof(GetById), new { id = sub.SubcategoriaId }, sub);
            }
            catch (ArgumentException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message });
            }
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPut("{id:int}")]
        [SwaggerOperation(Summary = "Actualizar subcategoría")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        [ProducesResponseType(StatusCodes.Status409Conflict)]
        public async Task<IActionResult> Update(int id, [FromBody] SubcategoriaUpdateRequest request)
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
            catch (ArgumentException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message });
            }
        }

        [Authorize(Roles = "administrador")]
        [HttpDelete("{id:int}")]
        [SwaggerOperation(Summary = "Eliminar subcategoría")]
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
        [HttpPut("{id:int}/activar")]
        [SwaggerOperation(Summary = "Activar subcategoría")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> Activar(int id)
        {
            try
            {
                await _service.ActivarAsync(id);
                return NoContent();
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPut("{id:int}/desactivar")]
        [SwaggerOperation(Summary = "Desactivar subcategoría")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> Desactivar(int id)
        {
            try
            {
                await _service.DesactivarAsync(id);
                return NoContent();
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }
    }
}
