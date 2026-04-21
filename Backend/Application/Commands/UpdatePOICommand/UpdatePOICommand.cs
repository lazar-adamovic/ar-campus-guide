using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Domain.Enums;
using MediatR;

namespace Application.Commands.UpdatePOICommand;

public record UpdatePOICommand(
    Guid Id,
    string Name,
    double Latitude,
    double Longitude,
    int CategoryId,
    string WebsiteUrl
) : IRequest;
