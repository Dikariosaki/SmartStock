using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using SmartStock.Application.Interfaces;
using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;
using Swashbuckle.AspNetCore.Annotations;

namespace SmartStock.API.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]")]
    public class UsuariosController : ControllerBase
    {
        private readonly IUsuarioService _service;

        public UsuariosController(IUsuarioService service)
        {
            _service = service;
        }

        [Authorize(Roles = "administrador")]
        [HttpGet]
        [SwaggerOperation(Summary = "Listar usuarios paginados", Description = "Obtiene usuarios con paginación y filtro de estado.")]
        [ProducesResponseType(typeof(PagedResponse<UsuarioResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<PagedResponse<UsuarioResponse>>> GetAll([FromQuery] PagedRequest request)
        {
            var response = await _service.GetPagedAsync(request);
            return Ok(response);
        }

        [Authorize(Roles = "administrador")]
        [HttpGet("{id:int}")]
        [SwaggerOperation(Summary = "Obtener usuario por id")]
        public async Task<ActionResult<UsuarioResponse>> GetById(int id)
        {
            var usuario = await _service.GetByIdAsync(id);
            if (usuario == null)
                return NotFound();
            return Ok(usuario);
        }

        [Authorize(Roles = "administrador")]
        [HttpPost]
        [SwaggerOperation(Summary = "Crear usuario")]
        public async Task<ActionResult<UsuarioResponse>> Create([FromBody] UsuarioCreateRequest request)
        {
            if (!ModelState.IsValid)
                return ValidationProblem(ModelState);

            try
            {
                var response = await _service.CreateAsync(request);
                return CreatedAtAction(nameof(GetById), new { id = response.UsuarioId }, response);
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message });
            }
        }

        [Authorize(Roles = "administrador,supervisor,auxiliar")]
        [HttpPut("{id:int}")]
        [SwaggerOperation(Summary = "Actualizar usuario")]
        public async Task<IActionResult> Update(int id, [FromBody] UsuarioUpdateRequest request)
        {
            if (!ModelState.IsValid)
                return ValidationProblem(ModelState);

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
        [SwaggerOperation(Summary = "Eliminar usuario")]
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
            catch (InvalidOperationException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [Authorize(Roles = "administrador")]
        [HttpPost("{id:int}/activate")]
        [SwaggerOperation(Summary = "Activar usuario")]
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

        [Authorize(Roles = "administrador")]
        [HttpPost("{id:int}/deactivate")]
        [SwaggerOperation(Summary = "Desactivar usuario")]
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
