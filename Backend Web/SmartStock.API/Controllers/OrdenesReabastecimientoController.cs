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
    public class OrdenesReabastecimientoController : ControllerBase
    {
        private readonly IOrdenReabastecimientoService _service;

        public OrdenesReabastecimientoController(IOrdenReabastecimientoService service)
        {
            _service = service;
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet]
        [SwaggerOperation(Summary = "Listar órdenes de reabastecimiento paginadas")]
        [ProducesResponseType(typeof(PagedResponse<OrdenReabastecimientoResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<PagedResponse<OrdenReabastecimientoResponse>>> GetAll([FromQuery] PagedRequest request)
        {
            var ordenes = await _service.GetPagedAsync(request);
            return Ok(ordenes);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet("{id:int}")]
        [SwaggerOperation(Summary = "Obtener orden por id")]
        public async Task<ActionResult<OrdenReabastecimientoResponse>> GetById(int id)
        {
            var orden = await _service.GetByIdAsync(id);
            if (orden == null) return NotFound();
            return Ok(orden);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet("proveedor/{proveedorId:int}")]
        [SwaggerOperation(Summary = "Listar órdenes por proveedor")]
        public async Task<ActionResult<IEnumerable<OrdenReabastecimientoResponse>>> GetByProveedor(int proveedorId)
        {
            var ordenes = await _service.GetByProveedorAsync(proveedorId);
            return Ok(ordenes);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPost]
        [SwaggerOperation(Summary = "Crear orden de reabastecimiento")]
        public async Task<ActionResult<OrdenReabastecimientoResponse>> Create([FromBody] OrdenReabastecimientoCreateRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);

            var response = await _service.CreateAsync(request);
            return CreatedAtAction(nameof(GetById), new { id = response.OrdenId }, response);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPut("{id:int}")]
        [SwaggerOperation(Summary = "Actualizar orden de reabastecimiento")]
        public async Task<IActionResult> Update(int id, [FromBody] OrdenReabastecimientoUpdateRequest request)
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
        [SwaggerOperation(Summary = "Eliminar orden de reabastecimiento")]
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
