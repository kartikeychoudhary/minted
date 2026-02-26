import {
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnInit,
    Output,
    SimpleChanges,
    ViewChild,
    ElementRef
} from '@angular/core';
import { ImageCroppedEvent, ImageCropperComponent, LoadedImage } from 'ngx-image-cropper';
import { MessageService } from 'primeng/api';

export type AvatarSize = 'sm' | 'md' | 'lg';

@Component({
    selector: 'app-avatar-upload',
    templateUrl: './avatar-upload.component.html',
    styleUrls: ['./avatar-upload.component.scss'],
    standalone: false
})
export class AvatarUploadComponent implements OnInit, OnChanges {

    /** Existing avatar shown as a data-URL or any image URL */
    @Input() currentAvatarUrl?: string | null;

    /** Initials to display when no avatar exists */
    @Input() initials: string = 'U';

    /** Background colour used for the initials fallback */
    @Input() color: string = '#6366f1';

    /** Label shown inside the dialog */
    @Input() label: string = 'Profile Picture';

    /** Size of the avatar preview ring: sm=40px, md=64px, lg=96px */
    @Input() size: AvatarSize = 'md';

    /** Emits a cropped File ready to be sent to the API */
    @Output() avatarSelected = new EventEmitter<File>();

    /** Emits when the user clicks "Remove" */
    @Output() avatarRemoved = new EventEmitter<void>();

    @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;

    showDialog = false;
    imageChangedEvent: Event | null = null;
    croppedImageBlob: Blob | null = null;
    croppedImageUrl: string = '';
    isCropping = false;

    // Pixel sizes for the avatar ring
    sizeMap: Record<AvatarSize, number> = { sm: 40, md: 64, lg: 96 };

    get pixelSize(): number {
        return this.sizeMap[this.size];
    }

    get fontSize(): number {
        return Math.floor(this.pixelSize * 0.38);
    }

    // Track internal preview separately so removal updates immediately
    previewUrl: string | null = null;

    ngOnInit(): void {
        this.previewUrl = this.currentAvatarUrl || null;
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['currentAvatarUrl']) {
            this.previewUrl = this.currentAvatarUrl || null;
        }
    }

    openPicker(): void {
        this.fileInputRef.nativeElement.value = '';
        this.fileInputRef.nativeElement.click();
    }

    onFileSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (!input.files?.length) return;

        const file = input.files[0];

        if (!file.type.startsWith('image/')) {
            return;
        }
        if (file.size > 2 * 1024 * 1024) {
            return;
        }

        this.imageChangedEvent = event;
        this.croppedImageBlob = null;
        this.croppedImageUrl = '';
        this.showDialog = true;
        this.isCropping = false;
    }

    imageCropped(event: ImageCroppedEvent): void {
        if (event.blob) {
            this.croppedImageBlob = event.blob;
        }
        if (event.objectUrl) {
            this.croppedImageUrl = event.objectUrl;
        }
    }

    imageLoaded(_image: LoadedImage): void {
        this.isCropping = false;
    }

    cropperReady(): void {
        this.isCropping = false;
    }

    loadImageFailed(): void {
        this.showDialog = false;
    }

    confirmCrop(): void {
        if (!this.croppedImageBlob) return;
        const file = new File([this.croppedImageBlob], 'avatar.jpg', { type: 'image/jpeg' });
        this.previewUrl = this.croppedImageUrl;
        this.avatarSelected.emit(file);
        this.showDialog = false;
    }

    cancelCrop(): void {
        this.imageChangedEvent = null;
        this.croppedImageBlob = null;
        this.croppedImageUrl = '';
        this.showDialog = false;
    }

    removeAvatar(): void {
        this.previewUrl = null;
        this.avatarRemoved.emit();
    }
}
