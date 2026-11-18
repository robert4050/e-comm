import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'priceFormat',
  standalone: true
})
export class PriceFormatPipe implements PipeTransform {
  transform(value: number | null | undefined, currencySymbol: string = '$'): string {
    if (value == null || isNaN(value)) return `${currencySymbol}0.00`;
    return `${currencySymbol}${value.toFixed(2)}`;
  }
}
