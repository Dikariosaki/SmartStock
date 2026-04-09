import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { HttpService } from '@core/services/http.service';
import { PagedResponse } from '@core/models/pagination.models';
import { config } from '@environments/environment';
import {
  BackendReporte,
  ReporteResumen,
} from '../models/reportes-evidencia.models';

@Injectable({
  providedIn: 'root',
})
export class ReportesService {
  private readonly apiUrl = config.api.endpoints.reportes;

  constructor(private readonly httpService: HttpService) {}

  getReportes(
    pageNumber: number = 1,
    pageSize: number = 6
  ): Observable<ReporteResumen[]> {
    return this.httpService
      .get<PagedResponse<BackendReporte>>(this.apiUrl, {
        pageNumber,
        pageSize,
        estado: true,
      })
      .pipe(
        map(response => response.data.map(report => this.mapBackendToFrontend(report)))
      );
  }

  private mapBackendToFrontend(report: BackendReporte): ReporteResumen {
    return {
      id: report.reporteId,
      title: report.titulo,
      description: report.descripcion,
      evidence: report.evidencia
        ? {
            ...report.evidencia,
            imageUrls: report.evidencia.imageUrls.map(imageUrl =>
              this.resolveEvidenceImageUrl(imageUrl)
            ),
          }
        : null,
      createdAt: new Date(report.fechaCreado),
      type: report.tipoReporte,
      status: report.estado,
    };
  }

  private resolveEvidenceImageUrl(imageUrl: string): string {
    if (!imageUrl) {
      return imageUrl;
    }

    if (/^https?:\/\//i.test(imageUrl)) {
      return imageUrl;
    }

    const normalizedBaseUrl = config.api.baseUrl.replace(/\/$/, '');
    const normalizedPath = imageUrl.startsWith('/') ? imageUrl : `/${imageUrl}`;
    return `${normalizedBaseUrl}${normalizedPath}`;
  }
}
