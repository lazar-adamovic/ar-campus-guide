using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using MediatR;

namespace Application.Queries.GetPOIDescriptionQuery;

public record GetPOIDescriptionQuery(Guid Id):IRequest<String>;

