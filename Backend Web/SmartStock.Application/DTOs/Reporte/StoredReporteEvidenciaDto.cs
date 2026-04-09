namespace SmartStock.Application.DTOs
{
    public class StoredReporteEvidenciaDto
    {
        public List<string> BlobKeys { get; set; } = [];
        public List<string> ImageUrls { get; set; } = [];
        public string? Observation { get; set; }

        public IEnumerable<string> AllImageReferences() =>
            BlobKeys
                .Concat(ImageUrls)
                .Select(value => value?.Trim())
                .Where(value => !string.IsNullOrWhiteSpace(value))
                .Cast<string>()
                .Distinct(StringComparer.Ordinal);
    }
}
