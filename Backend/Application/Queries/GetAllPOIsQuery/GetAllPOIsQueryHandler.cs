using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Domain.Interfaces;
using MediatR;

namespace Application.Queries.GetAllPOIsQuery;

public class GetAllPoisQueryHandler : IRequestHandler<GetAllPOIsQuery, List<PoiDto>>
{
    private readonly IPOIRepository _repository;

    public GetAllPoisQueryHandler(IPOIRepository repository)
    {
        _repository = repository;
    }

    public async Task<List<PoiDto>> Handle(GetAllPOIsQuery request, CancellationToken cancellationToken)
    {

        var pois = await _repository.GetAllPOIs();
        return pois.Select(p => new PoiDto(
            p.Id,
            p.Name,
            p.Latitude,
            p.Longitude,
            p.Description,
            p.WebsiteUrl,
            p.CategoryId,
            p.Category.Name,
            p.Category.ModelFileName, 
            p.Category.IconName


        )).ToList();
    }
}
