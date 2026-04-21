using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Domain.Interfaces;
using MediatR;

namespace Application.Commands.UpdatePOILocationCommand;

public class UpdatePOILocationCommandHandler : IRequestHandler<UpdatePOILocationCommand>
{
    private readonly IPOIRepository _repository;
    public UpdatePOILocationCommandHandler(IPOIRepository repository)
    {
        _repository = repository;
    }

    public async Task Handle(UpdatePOILocationCommand request, CancellationToken cancellationToken)
    {
        var poi = await _repository.GetPOIbyID(request.Id);

        if (poi == null)
        {
            throw new Exception("POI nije pronađena u bazi!");
        }

        poi.Latitude = request.Latitude;
        poi.Longitude = request.Longitude;

        await _repository.UpdatePOI(poi);
    }
}
