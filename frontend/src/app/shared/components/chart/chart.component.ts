import { Component, input, ElementRef, viewChild, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, ChartConfiguration, ChartType, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-chart',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="relative h-full w-full">
      <canvas #canvas></canvas>
    </div>
  `,
})
export class ChartComponent implements AfterViewInit, OnDestroy {
  canvas = viewChild<ElementRef<HTMLCanvasElement>>('canvas');
  
  type = input.required<ChartType>();
  data = input.required<ChartConfiguration['data']>();
  options = input<ChartConfiguration['options']>({});
  
  private chart: Chart | null = null;
  private isInitialized = false;

  ngAfterViewInit(): void {
    // Delay to ensure DOM is ready
    requestAnimationFrame(() => {
      this.createChart();
    });
  }

  ngOnDestroy(): void {
    if (this.chart) {
      this.chart.destroy();
      this.chart = null;
    }
    this.isInitialized = false;
  }

  private createChart(): void {
    if (this.isInitialized) return;
    
    try {
      const canvasEl = this.canvas()?.nativeElement;
      if (!canvasEl) {
        console.warn('ChartComponent: Canvas element not found');
        return;
      }

      // Check if canvas has dimensions
      if (canvasEl.offsetWidth === 0 || canvasEl.offsetHeight === 0) {
        console.warn('ChartComponent: Canvas has no dimensions, retrying...');
        setTimeout(() => this.createChart(), 100);
        return;
      }

      const currentData = this.data();
      const currentOptions = this.options();

      if (!currentData || !currentData.datasets || currentData.datasets.length === 0) {
        console.warn('ChartComponent: No data provided');
        return;
      }

      const config: ChartConfiguration = {
        type: this.type(),
        data: currentData, // Use data directly, no deep clone to avoid performance issues
        options: {
          ...currentOptions,
          responsive: true,
          maintainAspectRatio: false,
          animation: false, // Disable animations completely
          transitions: {
            active: {
              animation: {
                duration: 0
              }
            }
          },
          plugins: {
            legend: {
              position: 'top',
            },
          },
        },
      };

      this.chart = new Chart(canvasEl, config);
      this.isInitialized = true;
    } catch (error) {
      console.error('ChartComponent: Error creating chart', error);
      // Don't rethrow - just log the error to prevent crashes
    }
  }
}
