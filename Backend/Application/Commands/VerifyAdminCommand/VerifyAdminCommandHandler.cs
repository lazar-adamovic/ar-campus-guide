using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using MediatR;
using Microsoft.Extensions.Configuration;

namespace Application.Commands.VerifyAdminCommand;

public class VerifyAdminCommandHandler : IRequestHandler<VerifyAdminCommand, bool>
{
    private readonly IConfiguration _configuration;
    public VerifyAdminCommandHandler(IConfiguration configuration)
    {
        _configuration = configuration;
    }
    public async Task<bool> Handle(VerifyAdminCommand request, CancellationToken cancellationToken)
    {
        var correctPassword = _configuration["AdminSettings:AdminPassword"];

        if (string.IsNullOrEmpty(request.Password)) return false;

        bool isValid = request.Password == correctPassword;

        return await Task.FromResult(isValid);
    }
}
