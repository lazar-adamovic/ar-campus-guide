using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Domain.Models;
using Microsoft.EntityFrameworkCore;

namespace Infrastructure.Persistence;

public class ApplicationDbContext : DbContext, IApplicationDbContext
{
    public DbSet<POI> POIs { get; set; }
    public DbSet<Category> Categories { get; set; }
    public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options)
        : base(options)
    {
    }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<POI>(entity =>
        {
            entity.HasKey(e => e.Id);

            entity.Property(e => e.Name)
                .IsRequired()
                .HasMaxLength(200);

            entity.Property(e => e.Latitude).IsRequired();
            entity.Property(e => e.Longitude).IsRequired();
            entity.HasOne(p => p.Category)
                  .WithMany(c => c.POIs)
                  .HasForeignKey(p => p.CategoryId)
                  .OnDelete(DeleteBehavior.Restrict)
                  .IsRequired(false);

            entity.Property(e => e.Description)
                .HasMaxLength(200); 

            entity.Property(e => e.WebsiteUrl)
                .HasMaxLength(200); 
        });

        modelBuilder.Entity<Category>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Name).IsRequired().HasMaxLength(100);
            entity.Property(e => e.ModelFileName).IsRequired().HasMaxLength(200);
            entity.Property(e => e.IconName).HasMaxLength(100);
        });
        modelBuilder.Entity<Category>().HasData(
            new Category
            {
                Id = 1,
                Name = "Fakultet",
                ModelFileName = "fakultet.glb",
                IconName = "ic_fakultet"
            },
            new Category
            {
                Id = 2,
                Name = "Menza",
                ModelFileName = "menza.glb",
                IconName = "ic_menza"
            },
            new Category
            {
                Id = 3,
                Name = "Kafić",
                ModelFileName = "kafic.glb",
                IconName = "ic_kafic"
            },
            new Category
            {
                Id = 4,
                Name = "Služba",
                ModelFileName = "sluzba.glb",
                IconName = "ic_sluzba"
            },
            new Category
            {
                Id = 5,
                Name = "Znamenitost",
                ModelFileName = "znamenitost.glb",
                IconName = "ic_znamenitost"
            },
            new Category
            {
                Id = 6,
                Name = "Dom",
                ModelFileName = "dom.glb",
                IconName = "ic_dom"
            }
        );
    }
}