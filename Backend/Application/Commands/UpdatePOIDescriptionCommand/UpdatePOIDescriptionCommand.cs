using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using MediatR;

namespace Application.Commands.UpdatePOIDescriptionCommand;

public record UpdatePOIDescriptionCommand(Guid Id ,string Content):IRequest<Unit>;
