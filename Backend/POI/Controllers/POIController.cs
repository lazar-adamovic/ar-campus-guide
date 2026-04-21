using MediatR;
using Microsoft.AspNetCore.Mvc;
using Application;
using Application.Queries.GetAllPOIsQuery;
using Application.Commands.CreatePOICommand;
using Application.Commands.UpdatePOICommand;
using Application.Commands.UpdatePOILocationCommand;
using Application.Commands.DeletePOICommand;
using Microsoft.IdentityModel.Tokens;
using Application.Commands.VerifyAdminCommand;
using Application.Queries.GetCategories;
using Application.Commands.UpdatePOIDescriptionCommand;
using Application.Queries.GetPOIDescriptionQuery;
namespace POI.Controllers;

[ApiController]
public class POIController : ControllerBase
{
    private readonly IMediator _mediator;
    public POIController(IMediator mediator) { 
         _mediator = mediator; 
    }


    [HttpGet("POIList")]
    public async Task <IActionResult> GetPOIs()
    {
        var result = await _mediator.Send(new GetAllPOIsQuery());
        return Ok(result);
    }

    [HttpPost("CreatePOI")]
    public async Task <IActionResult> CreatePOI(CreatePoiCommand command)
    {
        var result = await _mediator.Send(command);
        return Ok(result);
    }

    [HttpPut("UpdatePOI/{id}")]
    public async Task<IActionResult> UpdatePOI(Guid id,UpdatePOICommand command)
    {
        await _mediator.Send(command);
        return NoContent();
    }

    [HttpPut("UpdateCoordinates/{id}")]
    public async Task<ActionResult> UpdateLocation(Guid id, [FromBody] UpdatePOILocationCommand command)
    {
        await _mediator.Send(command);
        return NoContent();
    }

    [HttpDelete("Delete/{id}")]
    public async Task<IActionResult> DeletePOI(Guid id)
    {
        await _mediator.Send(new DeletePOICommand(id));
        return NoContent();
    }

    [HttpPost("AdminPassword")]
    public async Task<IActionResult> VerifyAdmin(VerifyAdminCommand command)
    {
        var isAuthorized = await _mediator.Send(command);

        if (!isAuthorized)
        {
            return Unauthorized(new { message = "Pristup odbijen" });
        }

        return Ok(new { success = true });
    }
    [HttpGet("GetCategories")]
    public async Task<ActionResult<List<CategoryDto>>> GetAll()
    {
        var categories = await _mediator.Send(new GetCategoriesQuery());
        return Ok(categories);
    }

    [HttpPut("UpdateDescription/{id}")]
    public async Task<IActionResult> UpdateDescription(Guid id,UpdateDescriptionDto dto)
    {
        await _mediator.Send(new UpdatePOIDescriptionCommand(id,dto.Content));
        return NoContent();
    }

    [HttpGet("GetDescription/{id}")]
    public async Task<ActionResult<string>> GetDescription(Guid id)
    {
        var description = await _mediator.Send(new GetPOIDescriptionQuery(id));
        return Ok(description);
    }
}
