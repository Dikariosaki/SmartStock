using Microsoft.AspNetCore.Mvc;
using SmartStock.Application.DTOs;
using SmartStock.Application.DTOs.Common;
using SmartStock.Application.Interfaces;
using Swashbuckle.AspNetCore.Annotations;
using Microsoft.AspNetCore.Authorization;

namespace SmartStock.API.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]")]
    public class ClientesController : ControllerBase
    {
        private readonly IClienteService _service;

        public ClientesController(IClienteService service)
        {
            _service = service;
        }

        [Authorize(Roles = "administrador,supervisor,auxiliar")]
        [HttpGet]
        [SwaggerOperation(Summary = "Listar clientes")]
        [ProducesResponseType(typeof(IEnumerable<ClienteResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ClienteResponse>>> GetAll()
        {
            var clientes = await _service.GetAllAsync();
            return Ok(clientes);
        }

        [Authorize(Roles = "administrador,supervisor,auxiliar")]
        [HttpGet("{id:int}")]
        [SwaggerOperation(Summary = "Obtener cliente por id")]
        [ProducesResponseType(typeof(ClienteResponse), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<ClienteResponse>> GetById(int id)
        {
            var cliente = await _service.GetByIdAsync(id);
            if (cliente == null) return NotFound();
            return Ok(cliente);
        }

        [Authorize(Roles = "administrador,supervisor,auxiliar")]
        [HttpGet("usuario/{usuarioId:int}")]
        [SwaggerOperation(Summary = "Listar clientes por usuarioId")]
        [ProducesResponseType(typeof(IEnumerable<ClienteResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ClienteResponse>>> GetByUsuarioId(int usuarioId)
        {
            var clientes = await _service.GetByUsuarioIdAsync(usuarioId);
            return Ok(clientes);
        }

        [Authorize(Roles = "administrador")]
        [HttpPost]
        [SwaggerOperation(Summary = "Crear cliente")]
        [ProducesResponseType(typeof(ClienteResponse), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<ClienteResponse>> Create([FromBody] ClienteCreateRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);

            var cliente = await _service.CreateAsync(request);
            return CreatedAtAction(nameof(GetById), new { id = cliente.ClienteId }, cliente);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPut("{id:int}")]
        [SwaggerOperation(Summary = "Actualizar cliente")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> Update(int id, [FromBody] ClienteUpdateRequest request)
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
        [SwaggerOperation(Summary = "Eliminar cliente")]
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
