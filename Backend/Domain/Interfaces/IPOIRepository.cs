using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Domain.Models;
namespace Domain.Interfaces;

public interface IPOIRepository
{
    Task <IEnumerable<POI>> GetAllPOIs();
    Task <POI?> GetPOIbyID (Guid id);
    Task AddPOI (POI poi);
    Task DeletePOI (POI poi);
    Task UpdatePOI (POI poi);
    Task <List<Category>> GetCategories();
}
