using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Domain.Interfaces;
using Domain.Models;
using MediatR;
using Microsoft.Extensions.Configuration;

namespace Application.Commands.UpdatePOIDescriptionCommand;

public class UpdatePOIDescriptionCommandHandler : IRequestHandler<UpdatePOIDescriptionCommand, Unit>
{
    private readonly string _filesPath;
    private readonly IPOIRepository _repository;
    public UpdatePOIDescriptionCommandHandler(IConfiguration configuration,IPOIRepository repository)
    {
        _filesPath = configuration["FileSettings:PoisDescriptionPath"]!;
        _repository = repository;
    }

    public async Task<Unit> Handle(UpdatePOIDescriptionCommand request, CancellationToken cancellationToken)
    {
        var poi = await _repository.GetPOIbyID(request.Id);
        string safeName = string.Concat(poi.Name.Split(Path.GetInvalidFileNameChars()));
        string folderPath = Path.Combine(_filesPath, safeName);
        string filePath = Path.Combine(folderPath, $"{poi.Id}.txt");

        if (!Directory.Exists(folderPath)) Directory.CreateDirectory(folderPath);
        await File.WriteAllTextAsync(filePath, request.Content, cancellationToken);

        return Unit.Value;
    }
}
