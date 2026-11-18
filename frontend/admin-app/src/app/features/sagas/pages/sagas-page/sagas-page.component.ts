import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface SagaInstance {
  sagaId: string;
  orderNumber: string;
  customer: string;
  amount: number;
  currentStep: string;
  stepNumber: number;
  status: 'COMPLETED' | 'EXECUTING' | 'COMPENSATED';
  durationMs: number;
  timestamp: string;
}

@Component({
  selector: 'app-sagas-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sagas-page.component.html',
  styleUrls: ['./sagas-page.component.css']
})
export class SagasPageComponent implements OnInit {
  isSimulating: boolean = false;
  activeSimulationStep: number = 0;

  sagas: SagaInstance[] = [
    {
      sagaId: 'saga-98f21a',
      orderNumber: 'ORD-84920',
      customer: 'Alex Rivera',
      amount: 2499.99,
      currentStep: '5. OrderConfirmed & Kafka Dispatched',
      stepNumber: 5,
      status: 'COMPLETED',
      durationMs: 420,
      timestamp: 'Today, 14:22:04'
    },
    {
      sagaId: 'saga-77b34e',
      orderNumber: 'ORD-84921',
      customer: 'Marcus Vance',
      amount: 1099.00,
      currentStep: '5. OrderConfirmed & Kafka Dispatched',
      stepNumber: 5,
      status: 'COMPLETED',
      durationMs: 385,
      timestamp: 'Today, 11:05:12'
    },
    {
      sagaId: 'saga-66e89d',
      orderNumber: 'ORD-84922',
      customer: 'Sophia Chen',
      amount: 1899.00,
      currentStep: '5. OrderConfirmed & Kafka Dispatched',
      stepNumber: 5,
      status: 'COMPLETED',
      durationMs: 512,
      timestamp: 'Yesterday, 18:40:19'
    },
    {
      sagaId: 'saga-55c12f',
      orderNumber: 'ORD-84924',
      customer: 'Emma Watson',
      amount: 149.99,
      currentStep: '3. Inventory Locked -> Payment Declined',
      stepNumber: 4,
      status: 'COMPENSATED',
      durationMs: 650,
      timestamp: '14 Nov 2026'
    }
  ];

  ngOnInit(): void {}

  simulateSaga(): void {
    this.isSimulating = true;
    this.activeSimulationStep = 1;

    const interval = setInterval(() => {
      this.activeSimulationStep++;
      if (this.activeSimulationStep > 5) {
        clearInterval(interval);
        this.isSimulating = false;
        const newSaga: SagaInstance = {
          sagaId: 'saga-' + Math.random().toString(36).substring(2, 8),
          orderNumber: 'ORD-' + Math.floor(Math.random() * 90000 + 10000),
          customer: 'Staff Test User',
          amount: 349.50,
          currentStep: '5. OrderConfirmed & Kafka Dispatched',
          stepNumber: 5,
          status: 'COMPLETED',
          durationMs: 390,
          timestamp: 'Just now'
        };
        this.sagas.unshift(newSaga);
      }
    }, 700);
  }
}
