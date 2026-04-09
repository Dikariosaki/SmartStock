using System.Security.Cryptography;
using System.Text;
using BCrypt.Net;

namespace SmartStock.Application.Helpers
{
    public static class Hashing
    {
        // Bcrypt para nuevas contraseñas
        public static string BcryptHash(string input)
            => BCrypt.Net.BCrypt.HashPassword(input, workFactor: 11);

        public static bool BcryptVerify(string input, string hash)
            => BCrypt.Net.BCrypt.Verify(input, hash);

        // Compatibilidad temporal con MD5 para usuarios legados
        public static string Md5Hex(string input)
        {
            using var md5 = MD5.Create();
            var bytes = Encoding.UTF8.GetBytes(input);
            var hashBytes = md5.ComputeHash(bytes);
            var sb = new StringBuilder(hashBytes.Length * 2);
            foreach (var b in hashBytes)
                sb.Append(b.ToString("x2"));
            return sb.ToString();
        }
    }
}
