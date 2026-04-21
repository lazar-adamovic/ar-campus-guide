using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Domain.Models;
using MediatR;

namespace Application.Queries.GetCategories;

public record GetCategoriesQuery():IRequest<List<CategoryDto>>;

