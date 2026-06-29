export interface FloatingPanelPosition {
  top: number;
  left: number;
  width: number;
}

export function computeFloatingPanelPosition(
  trigger: HTMLElement,
  panel: HTMLElement | null,
  side: 'top' | 'bottom',
  align: 'start' | 'center' | 'end',
  gap = 4
): FloatingPanelPosition {
  const rect = trigger.getBoundingClientRect();
  const margin = 8;
  const viewportH = window.innerHeight;
  const viewportW = window.innerWidth;
  const panelHeight = panel && panel.offsetHeight > 0 ? panel.offsetHeight : 240;
  const panelWidth = panel && panel.offsetWidth > 0 ? panel.offsetWidth : rect.width;

  let top = side === 'bottom' ? rect.bottom + gap : rect.top - panelHeight - gap;

  if (side === 'bottom' && top + panelHeight > viewportH - margin) {
    top = rect.top - panelHeight - gap;
  } else if (side === 'top' && top < margin) {
    top = rect.bottom + gap;
  }

  let left = rect.left;
  if (align === 'center') {
    left = rect.left + rect.width / 2 - panelWidth / 2;
  } else if (align === 'end') {
    left = rect.right - panelWidth;
  }

  if (left + panelWidth > viewportW - margin) {
    left = viewportW - panelWidth - margin;
  }
  if (left < margin) {
    left = margin;
  }
  if (top + panelHeight > viewportH - margin) {
    top = viewportH - panelHeight - margin;
  }
  if (top < margin) {
    top = margin;
  }

  return { top, left, width: panelWidth };
}
