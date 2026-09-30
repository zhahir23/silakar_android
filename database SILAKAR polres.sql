-- Drop table

-- DROP TABLE public.dokumentasi;

CREATE TABLE public.dokumentasi (
	id serial4 NOT NULL,
	laporan_id int4 NULL,
	file_url text NOT NULL,
	created_at timestamp DEFAULT now() NULL,
	CONSTRAINT dokumentasi_file_url_not_null NOT NULL (file_url),
	CONSTRAINT dokumentasi_id_not_null NOT NULL (id),
	CONSTRAINT dokumentasi_pkey PRIMARY KEY (id),
	CONSTRAINT dokumentasi_laporan_id_fkey FOREIGN KEY (laporan_id) REFERENCES public.laporan(id) ON DELETE CASCADE
);

ALTER TABLE public.arsip ADD laporan_id int4 NULL;

ALTER TABLE public.laporan ADD judul varchar(255) NOT NULL;

ALTER TABLE public.laporan ADD jenis_kegiatan varchar(100) NULL;

ALTER TABLE public.log_aktivitas ADD keterangan text NULL;

ALTER TABLE public.arsip ADD nomor_arsip varchar(50) NULL;

ALTER TABLE public.users ADD nama varchar(100) NOT NULL;

ALTER TABLE public.users ADD atasan_id int4 NULL;

-- Drop table

-- DROP TABLE public.log_aktivitas;

CREATE TABLE public.log_aktivitas (
	id serial4 NOT NULL,
	user_id int4 NULL,
	aktivitas varchar(255) NOT NULL,
	keterangan text NULL,
	created_at timestamp DEFAULT now() NULL,
	CONSTRAINT log_aktivitas_aktivitas_not_null NOT NULL (aktivitas),
	CONSTRAINT log_aktivitas_id_not_null NOT NULL (id),
	CONSTRAINT log_aktivitas_pkey PRIMARY KEY (id),
	CONSTRAINT log_aktivitas_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);

ALTER TABLE public.laporan ADD catatan_atasan text NULL;

ALTER TABLE public.laporan ADD tanggal_kegiatan date NULL;

ALTER TABLE public.dokumentasi ADD file_url text NOT NULL;

ALTER TABLE public.laporan ADD status varchar(30) DEFAULT 'pending'::character varying NULL;

ALTER TABLE public.laporan ADD approved_by int4 NULL;

ALTER TABLE public.laporan ADD lokasi text NULL;

ALTER TABLE public.log_aktivitas ADD id serial4 NOT NULL;

-- Drop table

-- DROP TABLE public.users;

CREATE TABLE public.users (
	id serial4 NOT NULL,
	nama varchar(100) NOT NULL,
	email varchar(100) NOT NULL,
	"password" text NOT NULL,
	"role" varchar(20) NOT NULL,
	atasan_id int4 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT users_email_key UNIQUE (email),
	CONSTRAINT users_email_not_null NOT NULL (email),
	CONSTRAINT users_id_not_null NOT NULL (id),
	CONSTRAINT users_nama_not_null NOT NULL (nama),
	CONSTRAINT users_password_not_null NOT NULL ("password"),
	CONSTRAINT users_pkey PRIMARY KEY (id),
	CONSTRAINT users_role_not_null NOT NULL ("role")
);

ALTER TABLE public.notifikasi ADD id serial4 NOT NULL;

ALTER TABLE public.notifikasi ADD pesan text NOT NULL;

ALTER TABLE public.laporan ADD approved_at timestamp NULL;

ALTER TABLE public.dokumentasi ADD laporan_id int4 NULL;

ALTER TABLE public.laporan ADD user_id int4 NULL;

ALTER TABLE public.users ADD email varchar(100) NOT NULL;

ALTER TABLE public.dokumentasi ADD id serial4 NOT NULL;

ALTER TABLE public.log_aktivitas ADD created_at timestamp DEFAULT now() NULL;

ALTER TABLE public.users ADD "role" varchar(20) NOT NULL;

ALTER TABLE public.laporan ADD deskripsi text NULL;

ALTER TABLE public.arsip ADD tanggal_arsip timestamp DEFAULT now() NULL;

ALTER TABLE public.users ADD id serial4 NOT NULL;

ALTER TABLE public.dokumentasi ADD created_at timestamp DEFAULT now() NULL;

ALTER TABLE public.log_aktivitas ADD user_id int4 NULL;

ALTER TABLE public.log_aktivitas ADD aktivitas varchar(255) NOT NULL;

ALTER TABLE public.notifikasi ADD user_id int4 NULL;

ALTER TABLE public.users ADD "password" text NOT NULL;

ALTER TABLE public.users ADD created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL;

ALTER TABLE public.laporan ADD id serial4 NOT NULL;

ALTER TABLE public.laporan ADD created_at timestamp DEFAULT now() NULL;

ALTER TABLE public.notifikasi ADD dibaca bool DEFAULT false NULL;