using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Domain.Interfaces;
using Domain.Models;
using MediatR;
using Microsoft.Extensions.Configuration;
namespace Application.Commands.CreatePOICommand;

public class CreatePOICommandHandler : IRequestHandler<CreatePoiCommand,Guid>
{
    private readonly IPOIRepository _repository;
    private readonly string _filesPath;
    public CreatePOICommandHandler(IPOIRepository repository, IConfiguration configuration)
    {
        _filesPath = configuration["FileSettings:PoisDescriptionPath"]!;
        _repository = repository;
    }

    public async Task<Guid> Handle(CreatePoiCommand request, CancellationToken cancellationToken)
    {
         
        var poi = new POI{

            Id = Guid.NewGuid(), 
            Name = request.Name,
            Latitude = request.Latitude,
            Longitude = request.Longitude,
            CategoryId = request.CategoryId,
            Description = "",
            WebsiteUrl = request.WebsiteUrl
        };

        await _repository.AddPOI(poi);

        string safeName = string.Concat(poi.Name.Split(Path.GetInvalidFileNameChars()));
        string folderPath = Path.Combine(_filesPath, safeName);
        string filePath = Path.Combine(folderPath, $"{poi.Id}.txt");

        if (!Directory.Exists(folderPath))
        {
            Directory.CreateDirectory(folderPath);
        }
        await File.WriteAllTextAsync(filePath, string.Empty, cancellationToken);
        return poi.Id;
    }
}
