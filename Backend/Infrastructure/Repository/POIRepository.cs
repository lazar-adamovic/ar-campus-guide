using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Domain.Interfaces;
using Domain.Models;
using Infrastructure.Persistence;
using Microsoft.EntityFrameworkCore;
namespace Infrastructure.Repository;

public class POIRepository : IPOIRepository
{
    private readonly IApplicationDbContext _context;
    public POIRepository (IApplicationDbContext context)
    {
        _context = context;
    }
    public async Task AddPOI(POI poi)
    {
        await _context.POIs.AddAsync(poi);
        await _context.SaveChangesAsync();
    }

    public async Task DeletePOI(POI poi)
    {
        _context.POIs.Remove(poi);
        await _context.SaveChangesAsync();
    }

    public async Task<IEnumerable<POI>> GetAllPOIs()
    {
        return await _context.POIs
            .Include(p => p.Category)
            .ToListAsync();
    }

    public async Task<List<Category>> GetCategories()
    {
        return await _context.Categories.ToListAsync();
    }

    public async Task<POI?> GetPOIbyID(Guid id)
    {
        return await _context.POIs.FirstOrDefaultAsync(x => x.Id == id);
    }

    public async Task UpdatePOI(POI poi)
    {
        _context.POIs.Update(poi);
        await _context.SaveChangesAsync();
    }
}
