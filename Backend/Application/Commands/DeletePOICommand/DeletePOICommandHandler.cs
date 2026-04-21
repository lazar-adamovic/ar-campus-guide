using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Domain.Interfaces;
using MediatR;

namespace Application.Commands.DeletePOICommand;

public class DeletePOICommandHandler: IRequestHandler<DeletePOICommand>
{
    private readonly IPOIRepository _repository;
    public DeletePOICommandHandler(IPOIRepository repository)
    {
        _repository = repository;
    }

    public async Task Handle(DeletePOICommand request, CancellationToken cancellationToken)
    {
        var poi = await _repository.GetPOIbyID(request.Id);
        if (poi == null) {
            throw new Exception("POI sa tim ID nije pronađen!");
        }
        else
        {
            await _repository.DeletePOI(poi);
        }
    }
}
