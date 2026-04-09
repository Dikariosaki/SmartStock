using System.ComponentModel.DataAnnotations;

namespace SmartStock.Application.DTOs
{
    public class AuthLoginRequest
    {
        [Required]
        [EmailAddress]
        public string Email { get; set; } = string.Empty;

        [Required]
        public string Password { get; set; } = string.Empty;
    }
}
