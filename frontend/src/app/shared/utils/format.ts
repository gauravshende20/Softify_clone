export function formatCount(value: number | null | undefined): string {
  if (value == null) {
    return '0';
  }
  if (value < 1000) {
    return String(value);
  }
  if (value < 1_000_000) {
    return `${(value / 1000).toFixed(value < 10_000 ? 1 : 0)}K`;
  }
  return `${(value / 1_000_000).toFixed(value < 10_000_000 ? 1 : 0)}M`;
}

export function coverGradient(seed: string | undefined): string {
  const text = seed ?? 'harmonia';
  let hash = 0;
  for (let i = 0; i < text.length; i++) {
    hash = text.charCodeAt(i) + ((hash << 5) - hash);
  }
  const hue = Math.abs(hash) % 360;
  return `linear-gradient(145deg, hsl(${hue} 28% 22%), hsl(${(hue + 40) % 360} 32% 12%))`;
}

export function initials(name: string | undefined): string {
  if (!name) {
    return 'H';
  }
  return name
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase();
}

export function httpMessage(error: unknown, fallback = 'Something went wrong'): string {
  if (typeof error === 'object' && error && 'error' in error) {
    const body = (error as { error?: { message?: string } }).error;
    if (body?.message) {
      return body.message;
    }
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}
