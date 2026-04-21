using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using MediatR;
namespace Application.Queries.GetAllPOIsQuery;

public record GetAllPOIsQuery(string? TypeFilter = null) : IRequest<List<PoiDto>>;

