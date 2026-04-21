using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using MediatR;

namespace Application.Commands.VerifyAdminCommand;
public record VerifyAdminCommand(string? Password):IRequest<bool>;
