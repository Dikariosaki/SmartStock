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
    public class RolesController : ControllerBase
    {
        private readonly IRolService _service;

        public RolesController(IRolService service)
        {
            _service = service;
        }

        [Authorize(Roles = "administrador")]
        [HttpGet]
        [SwaggerOperation(Summary = "Listar roles paginados")]
        [ProducesResponseType(typeof(PagedResponse<RolResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<PagedResponse<RolResponse>>> GetAll([FromQuery] PagedRequest request)
        {
            var roles = await _service.GetPagedAsync(request);
            return Ok(roles);
        }

        [Authorize(Roles = "administrador")]
        [HttpGet("{id:int}")]
        [SwaggerOperation(Summary = "Obtener rol por id")]
        public async Task<ActionResult<RolResponse>> GetById(int id)
        {
            var rol = await _service.GetByIdAsync(id);
            if (rol == null) return NotFound();
            return Ok(rol);
        }

        [Authorize(Roles = "administrador")]
        [HttpPost]
        [SwaggerOperation(Summary = "Crear rol")]
        public async Task<ActionResult<RolResponse>> Create([FromBody] RolCreateRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);

            try
            {
                var response = await _service.CreateAsync(request);
                return CreatedAtAction(nameof(GetById), new { id = response.RolId }, response);
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message });
            }
        }

        [Authorize(Roles = "administrador")]
        [HttpPut("{id:int}")]
        [SwaggerOperation(Summary = "Actualizar rol")]
        public async Task<IActionResult> Update(int id, [FromBody] RolUpdateRequest request)
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
        [SwaggerOperation(Summary = "Eliminar rol")]
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
    }
}
