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
    public class ProveedoresController : ControllerBase
    {
        private readonly IProveedorService _service;

        public ProveedoresController(IProveedorService service)
        {
            _service = service;
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet]
        [SwaggerOperation(Summary = "Listar proveedores paginados", Description = "Obtiene proveedores con paginación y filtro de estado (via Usuario).")]
        [ProducesResponseType(typeof(PagedResponse<ProveedorResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<PagedResponse<ProveedorResponse>>> GetAll([FromQuery] PagedRequest request)
        {
            var response = await _service.GetPagedAsync(request);
            return Ok(response);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet("{id:int}")]
        [SwaggerOperation(Summary = "Obtener proveedor por id")]
        [ProducesResponseType(typeof(ProveedorResponse), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<ProveedorResponse>> GetById(int id)
        {
            var proveedor = await _service.GetByIdAsync(id);
            if (proveedor == null) return NotFound();
            return Ok(proveedor);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet("usuario/{usuarioId:int}")]
        [SwaggerOperation(Summary = "Listar proveedores por usuarioId")]
        [ProducesResponseType(typeof(IEnumerable<ProveedorResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ProveedorResponse>>> GetByUsuarioId(int usuarioId)
        {
            var proveedores = await _service.GetByUsuarioIdAsync(usuarioId);
            return Ok(proveedores);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPost]
        [SwaggerOperation(Summary = "Crear proveedor")]
        [ProducesResponseType(typeof(ProveedorResponse), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<ProveedorResponse>> Create([FromBody] ProveedorCreateRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);

            var proveedor = await _service.CreateAsync(request);
            return CreatedAtAction(nameof(GetById), new { id = proveedor.ProveedorId }, proveedor);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPut("{id:int}")]
        [SwaggerOperation(Summary = "Actualizar proveedor")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> Update(int id, [FromBody] ProveedorUpdateRequest request)
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
        [SwaggerOperation(Summary = "Eliminar proveedor")]
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
    }
}
