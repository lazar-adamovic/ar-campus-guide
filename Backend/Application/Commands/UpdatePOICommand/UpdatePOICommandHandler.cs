using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Domain.Interfaces;
using MediatR;
using Microsoft.Extensions.Configuration;

namespace Application.Commands.UpdatePOICommand;

public class UpdatePOICommandHandler : IRequestHandler<UpdatePOICommand>
{
    private readonly IPOIRepository _repository;
    private readonly string _filesPath;
    public UpdatePOICommandHandler(IPOIRepository repository,IConfiguration configuration)
    {
        _filesPath = configuration["FileSettings:PoisDescriptionPath"]!;
        _repository = repository;
    }

    public async Task Handle(UpdatePOICommand request, CancellationToken cancellationToken)
    {
        var poi = await _repository.GetPOIbyID(request.Id);
        string oldSafeName = string.Concat(poi.Name.Split(Path.GetInvalidFileNameChars()));
        string newSafeName = string.Concat(request.Name.Split(Path.GetInvalidFileNameChars()));

        string oldFolderPath = Path.Combine(_filesPath, oldSafeName);
        string newFolderPath = Path.Combine(_filesPath, newSafeName);
        if (oldSafeName != newSafeName && Directory.Exists(oldFolderPath))
        {
            if (Directory.Exists(newFolderPath))
            {
                Directory.Delete(newFolderPath, true);
            }
            Directory.Move(oldFolderPath, newFolderPath);
        }
        else if (!Directory.Exists(newFolderPath))
        {
            Directory.CreateDirectory(newFolderPath);
        }
        if (poi == null)
        {
            throw new Exception("POI nije pronađen u bazi!");
        }
        poi.Name = request.Name;
        poi.Latitude = request.Latitude;
        poi.Longitude = request.Longitude;
        poi.CategoryId = request.CategoryId;
        poi.WebsiteUrl = request.WebsiteUrl;

        await _repository.UpdatePOI(poi);

    }
}

