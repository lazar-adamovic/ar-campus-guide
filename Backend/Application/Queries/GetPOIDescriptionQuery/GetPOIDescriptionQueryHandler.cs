using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Domain.Interfaces;
using MediatR;
using Microsoft.Extensions.Configuration;

namespace Application.Queries.GetPOIDescriptionQuery;

public class GetPOIDescriptionQueryHandler:IRequestHandler<GetPOIDescriptionQuery,string>
{
    private readonly string _filesPath;
    private readonly IPOIRepository _repository;
    public GetPOIDescriptionQueryHandler(IConfiguration configuration,IPOIRepository repository)
    {
        _filesPath = configuration["FileSettings:PoisDescriptionPath"]!;
        _repository = repository;
    }

    public async Task<string> Handle(GetPOIDescriptionQuery request, CancellationToken cancellationToken)
    {
        var poi = await _repository.GetPOIbyID(request.Id);
        if (poi == null)
        {
            return "Opis nije pronađen (POI ne postoji).";
        }

        string safeName = string.Concat(poi.Name.Split(Path.GetInvalidFileNameChars()));
        string folderPath = Path.Combine(_filesPath, safeName);
        string filePath = Path.Combine(folderPath, $"{poi.Id}.txt");

        if (!File.Exists(filePath))
        {
            return "";
        }
        return await File.ReadAllTextAsync(filePath, cancellationToken);
    }
}
