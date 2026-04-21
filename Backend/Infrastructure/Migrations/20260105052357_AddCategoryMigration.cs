using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

#pragma warning disable CA1814 // Prefer jagged arrays over multidimensional

namespace Infrastructure.Migrations
{
    /// <inheritdoc />
    public partial class AddCategoryMigration : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "CategoryId",
                table: "POIs",
                type: "int",
                nullable: true);

            migrationBuilder.CreateTable(
                name: "Categories",
                columns: table => new
                {
                    Id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    Name = table.Column<string>(type: "nvarchar(100)", maxLength: 100, nullable: false),
                    ModelFileName = table.Column<string>(type: "nvarchar(200)", maxLength: 200, nullable: false),
                    IconName = table.Column<string>(type: "nvarchar(100)", maxLength: 100, nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Categories", x => x.Id);
                });

            migrationBuilder.InsertData(
                table: "Categories",
                columns: new[] { "Id", "IconName", "ModelFileName", "Name" },
                values: new object[,]
                {
                    { 1, "ic_fakultet", "fakultet.glb", "Fakultet" },
                    { 2, "ic_menza", "menza.glb", "Menza" },
                    { 3, "ic_kafic", "kafic.glb", "Kafić" },
                    { 4, "ic_sluzba", "sluzba.glb", "Služba" },
                    { 5, "ic_znamenitost", "znamenitost.glb", "Znamenitost" },
                    { 6, "ic_dom", "dom.glb", "Dom" }
                });

            migrationBuilder.CreateIndex(
                name: "IX_POIs_CategoryId",
                table: "POIs",
                column: "CategoryId");

            migrationBuilder.AddForeignKey(
                name: "FK_POIs_Categories_CategoryId",
                table: "POIs",
                column: "CategoryId",
                principalTable: "Categories",
                principalColumn: "Id",
                onDelete: ReferentialAction.Restrict);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_POIs_Categories_CategoryId",
                table: "POIs");

            migrationBuilder.DropTable(
                name: "Categories");

            migrationBuilder.DropIndex(
                name: "IX_POIs_CategoryId",
                table: "POIs");

            migrationBuilder.DropColumn(
                name: "CategoryId",
                table: "POIs");
        }
    }
}
