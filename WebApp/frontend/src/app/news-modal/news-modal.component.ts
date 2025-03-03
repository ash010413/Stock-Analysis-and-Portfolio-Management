import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'app-news-modal',
    templateUrl: './news-modal.component.html',
    styleUrls: ['./news-modal.component.css']
})
export class NewsModalComponent {
  @Input() source: string = '';
  @Input() publishedDate: string = '';
  @Input() headline: string = '';
  @Input() summary: string = '';
  @Input() url: string = '';

    constructor(public activeModal: NgbActiveModal) { }

    shareOnTwitter() {
        window.open('https://twitter.com/share?url=' + encodeURIComponent(this.url) + '&text=' + encodeURIComponent(this.headline), '_blank');
    }
    
    shareOnFacebook() {
        window.open('https://www.facebook.com/sharer/sharer.php?u=' + encodeURIComponent(this.url), '_blank');
    }

    formatPublishedDate(dateString: string): string {
        const date = new Date(dateString);
        const options: Intl.DateTimeFormatOptions = { month: 'long', day: 'numeric', year: 'numeric' };
        return date.toLocaleDateString('en-US', options);
    }

    closeModal() {
        this.activeModal.close();
    }
}

