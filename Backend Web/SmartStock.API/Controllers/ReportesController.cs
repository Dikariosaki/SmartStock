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
    public class ReportesController : ControllerBase
    {
        private readonly IReporteService _service;

        public ReportesController(IReporteService service)
        {
            _service = service;
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet]
        [SwaggerOperation(Summary = "Listar reportes paginados", Description = "Obtiene reportes con paginación y filtro de estado.")]
        [ProducesResponseType(typeof(PagedResponse<ReporteResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<PagedResponse<ReporteResponse>>> GetAll([FromQuery] PagedRequest request)
        {
            var items = await _service.GetPagedAsync(request);
            return Ok(items);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpGet("{id:int}")]
        [SwaggerOperation(Summary = "Obtener reporte por id")]
        [ProducesResponseType(typeof(ReporteResponse), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<ReporteResponse>> GetById(int id)
        {
            var item = await _service.GetByIdAsync(id);
            if (item == null) return NotFound();
            return Ok(item);
        }

        [AllowAnonymous]
        [HttpGet("{id:int}/evidencia/imagenes/{imageIndex:int}")]
        [SwaggerOperation(Summary = "Obtener una imagen de evidencia del reporte")]
        [ProducesResponseType(typeof(FileContentResult), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> GetEvidenceImage(int id, int imageIndex)
        {
            var image = await _service.GetEvidenceImageAsync(id, imageIndex);
            if (image is null) return NotFound();
            return File(image.Content, image.ContentType);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPost]
        [SwaggerOperation(Summary = "Crear reporte")]
        [ProducesResponseType(typeof(ReporteResponse), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<ReporteResponse>> Create([FromBody] ReporteCreateRequest request)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);

            var response = await _service.CreateAsync(request);
            return CreatedAtAction(nameof(GetById), new { id = response.ReporteId }, response);
        }

        [Authorize(Roles = "administrador,supervisor")]
        [HttpPut("{id:int}")]
        [SwaggerOperation(Summary = "Actualizar reporte")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> Update(int id, [FromBody] ReporteUpdateRequest request)
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
        [SwaggerOperation(Summary = "Eliminar reporte")]
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

        [Authorize(Roles = "administrador")]
        [HttpPost("{id:int}/activate")]
        [SwaggerOperation(Summary = "Activar reporte")]
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
        [SwaggerOperation(Summary = "Desactivar reporte")]
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
