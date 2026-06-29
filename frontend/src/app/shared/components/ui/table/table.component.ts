import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { mergeClasses } from '@/shared/utils/merge-classes';

@Component({
  selector: 'app-table',
  imports: [CommonModule],
  template: `
    <div class="relative w-full overflow-auto">
      <table [class]="mergeClasses('w-full caption-bottom text-sm', className())">
        <ng-content />
      </table>
    </div>
  `,
})
export class TableComponent {
  className = input('', { alias: 'class' });
  mergeClasses = mergeClasses;
}

@Component({
  selector: 'app-table-header',
  imports: [CommonModule],
  host: { class: 'contents' },
  template: `
    <thead [class]="mergeClasses('[&_tr]:border-b', className())">
      <ng-content />
    </thead>
  `,
})
export class TableHeaderComponent {
  className = input('', { alias: 'class' });
  mergeClasses = mergeClasses;
}

@Component({
  selector: 'app-table-body',
  imports: [CommonModule],
  host: { class: 'contents' },
  template: `
    <tbody [class]="mergeClasses('[&_tr:last-child]:border-0', className())">
      <ng-content />
    </tbody>
  `,
})
export class TableBodyComponent {
  className = input('', { alias: 'class' });
  mergeClasses = mergeClasses;
}

@Component({
  selector: 'app-table-footer',
  imports: [CommonModule],
  host: { class: 'contents' },
  template: `
    <tfoot [class]="mergeClasses('border-t bg-muted/50 font-medium [&>tr]:last:border-b-0', className())">
      <ng-content />
    </tfoot>
  `,
})
export class TableFooterComponent {
  className = input('', { alias: 'class' });
  mergeClasses = mergeClasses;
}

@Component({
  selector: 'app-table-row',
  imports: [CommonModule],
  host: {
    class: 'contents',
    '[attr.data-state]': 'selected() ? "selected" : null',
  },
  template: `
    <tr
      [class]="
        mergeClasses(
          'border-b transition-colors hover:bg-muted/50 data-[state=selected]:bg-muted',
          className()
        )
      ">
      <ng-content />
    </tr>
  `,
})
export class TableRowComponent {
  className = input('', { alias: 'class' });
  selected = input(false);
  mergeClasses = mergeClasses;
}

@Component({
  selector: 'app-table-head',
  imports: [CommonModule],
  host: { class: 'contents' },
  template: `
    <th
      [class]="
        mergeClasses(
          'h-10 px-2 text-left align-middle font-medium text-muted-foreground [&:has([role=checkbox])]:pr-0',
          className()
        )
      ">
      <ng-content />
    </th>
  `,
})
export class TableHeadComponent {
  className = input('', { alias: 'class' });
  mergeClasses = mergeClasses;
}

@Component({
  selector: 'app-table-cell',
  imports: [CommonModule],
  host: { class: 'contents' },
  template: `
    <td
      [attr.colspan]="colspan() ?? null"
      [class]="mergeClasses('p-2 align-middle [&:has([role=checkbox])]:pr-0', className())">
      <ng-content />
    </td>
  `,
})
export class TableCellComponent {
  className = input('', { alias: 'class' });
  colspan = input<number | null>(null);
  mergeClasses = mergeClasses;
}

@Component({
  selector: 'app-table-caption',
  imports: [CommonModule],
  template: `
    <caption [class]="mergeClasses('mt-4 text-sm text-muted-foreground', className())">
      <ng-content />
    </caption>
  `,
})
export class TableCaptionComponent {
  className = input('', { alias: 'class' });
  mergeClasses = mergeClasses;
}
