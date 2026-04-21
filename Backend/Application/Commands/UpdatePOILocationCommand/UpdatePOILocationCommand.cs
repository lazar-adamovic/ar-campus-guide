using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using MediatR;

namespace Application.Commands.UpdatePOILocationCommand;

public record UpdatePOILocationCommand(Guid Id, double Latitude,double Longitude):IRequest;
