using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Application;

public record PoiDto(
    Guid Id,
    string Name,
    double Latitude,
    double Longitude,
    string? Description,
    string? WebsiteUrl,
    int? CategoryId,
    string CategoryName,
    string ModelFileName,
    string IconName
);
