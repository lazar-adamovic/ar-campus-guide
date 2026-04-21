using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Domain.Interfaces;
using Domain.Models;
using MediatR;

namespace Application.Queries.GetCategories;

public class GetCategoriesQueryHandler : IRequestHandler<GetCategoriesQuery, List<CategoryDto>>
{
    private readonly IPOIRepository _repository;
    public GetCategoriesQueryHandler(IPOIRepository repository)
    {
        _repository = repository;
    }

    public async Task<List<CategoryDto>> Handle(GetCategoriesQuery request, CancellationToken cancellationToken)
    {
        var categories = await _repository.GetCategories();
        return categories.Select(c => new CategoryDto(
            c.Id,
            c.Name,
            c.ModelFileName,
            c.IconName
        )).ToList();
    }
}
