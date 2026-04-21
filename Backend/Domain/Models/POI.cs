using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Domain.Enums;
namespace Domain.Models;

public class POI
{
    public required Guid Id { get; init; }
    public required string Name { get; set; }
    public required double Latitude { get; set; }
    public required double Longitude { get; set; }
    public string? Description { get; set; }
    public string? WebsiteUrl { get; set; }
    public int? CategoryId { get; set; }
    public virtual Category? Category { get; set; } = null!;
}
