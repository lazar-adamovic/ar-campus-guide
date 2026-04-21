using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Domain.Models;

public class Category
{
    public int Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string ModelFileName { get; set; } = string.Empty;
    public string IconName { get; set; } = string.Empty;
    public virtual ICollection<POI> POIs { get; set; } = new List<POI>();
}
