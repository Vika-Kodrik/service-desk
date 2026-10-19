-- ============================================================
-- Тестовые данные приложения «Учёт заявок в service desk».
-- Выполняется при каждом старте (ddl-auto=create пересоздаёт схему,
-- затем скрипт засеивает данные заново — БД всегда в известном состоянии).
-- Даты относительные (DATEADD от CURRENT_TIMESTAMP), поэтому статистика
-- «время решения / нарушение SLA» живая при любом запуске.
-- ============================================================

-- ---------- Пользователи (пароли: admin / 12345 / 12345 / viewer) ----------
INSERT INTO users (id, username, password) VALUES
  (1, 'admin',   '$2a$10$fSR0gFzbNsgxKC9I1.03hOOhNTwEciuMeILpyP.TaN2jKnhF3O21.'),
  (2, 'ivanov',  '$2a$10$eXeIjfr/oEOWC0AvIo0iS.HS7xbtw61xcZRY5G/ciTOScGWkHSxaC'),
  (3, 'petrov',  '$2a$10$eXeIjfr/oEOWC0AvIo0iS.HS7xbtw61xcZRY5G/ciTOScGWkHSxaC'),
  (4, 'viewer',  '$2a$10$wKxpx6p/cCsdEvezltjWXOCOh5TLH8uv12Z3sdowD2Tyjbp4g18BO');

INSERT INTO user_roles (user_id, role) VALUES
  (1, 'ADMIN'),
  (2, 'USER'),
  (3, 'USER'),
  (4, 'READ_ONLY');

-- ---------- Справочник категорий с SLA ----------
INSERT INTO categories (id, name, sla_hours, department, contact_phone) VALUES
  (1, 'Проблема с ПК',             24, 'Отдел техподдержки', '+7 (343) 000-00-01'),
  (2, 'Доступ и учётные записи',    8, 'Служба ИБ',          '+7 (343) 000-00-02'),
  (3, 'Программное обеспечение',   48, 'Отдел разработки',   '+7 (343) 000-00-03'),
  (4, 'Сеть и связь',              12, 'Отдел связи',        '+7 (343) 000-00-04'),
  (5, 'Прочее',                    72, 'Отдел техподдержки', '+7 (343) 000-00-05');

-- ---------- Заявки ----------
-- Сценарий: у завершённых заявок есть как уложившиеся в SLA,
-- так и просроченные (№1: 30 ч при SLA 8 ч; №6: 30 ч при SLA 24 ч).
INSERT INTO requests (id, title, description, status, priority, created_at, completed_at, author_id, category_id) VALUES
  (1, 'Заблокирован аккаунт после отпуска',
      'После возвращения из отпуска не удаётся войти в домен, пишет «учётная запись заблокирована». Разблокируйте, пожалуйста.',
      'COMPLETED', 'HIGH',
      DATEADD('DAY', -6, CURRENT_TIMESTAMP), DATEADD('HOUR', 30, DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
      2, 2),

  (2, 'Не включается рабочий компьютер',
      'Системный блок пищит, на мониторе нет сигнала. Работа стоит, требуется срочный выезд специалиста.',
      'COMPLETED', 'CRITICAL',
      DATEADD('DAY', -5, CURRENT_TIMESTAMP), DATEADD('HOUR', 6, DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
      2, 1),

  (3, 'Нет доступа к сетевой папке отдела',
      'Пропал доступ к папке \\fileserver\otdel, коллеги заходят нормально.',
      'COMPLETED', 'MEDIUM',
      DATEADD('DAY', -4, CURRENT_TIMESTAMP), DATEADD('HOUR', 5, DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
      2, 2),

  (4, 'Медленно работает Outlook',
      'Письма открываются по 10–15 секунд, поиск подвисает. Просьба посмотреть.',
      'IN_PROGRESS', 'LOW',
      DATEADD('DAY', -3, CURRENT_TIMESTAMP), NULL,
      2, 3),

  (5, 'Сломан принтер в переговорной 4Б',
      'HP LaserJet заминает бумагу и печатает пустые страницы.',
      'NEW', 'MEDIUM',
      DATEADD('DAY', -1, CURRENT_TIMESTAMP), NULL,
      2, 1),

  (6, 'Вирусное предупреждение на ноутбуке',
      'Антивирус сообщил об обнаружении трояна и ушёл в карантин. Ноутбук отключён от сети.',
      'COMPLETED', 'CRITICAL',
      DATEADD('DAY', -4, CURRENT_TIMESTAMP), DATEADD('HOUR', 30, DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
      3, 1),

  (7, 'Заявка на лицензию MS Project',
      'Для нового проекта нужна лицензия MS Project Professional, согласование с руководителем получено.',
      'IN_PROGRESS', 'MEDIUM',
      DATEADD('DAY', -2, CURRENT_TIMESTAMP), NULL,
      3, 3),

  (8, 'Не работает Wi-Fi на 3 этаже',
      'Точка доступа в конце коридора не раздаёт сеть, телефоны сотрудников отваливаются.',
      'COMPLETED', 'HIGH',
      DATEADD('DAY', -7, CURRENT_TIMESTAMP), DATEADD('HOUR', 10, DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
      3, 4),

  (9, 'Перенос рабочего телефона на новое место',
      'Переехал в кабинет 305, нужно переподключить телефонный аппарат.',
      'CANCELLED', 'LOW',
      DATEADD('DAY', -8, CURRENT_TIMESTAMP), NULL,
      3, 4),

  (10, 'Ошибка в 1С при формировании отчёта',
      'При выгрузке оборотно-сальдовой ведомости появляется «Ошибка доступа к файловой базе», отчёт не формируется.',
      'NEW', 'HIGH',
      DATEADD('HOUR', -12, CURRENT_TIMESTAMP), NULL,
      2, 3);

-- ---------- Синхронизация счётчиков IDENTITY после ручной вставки ----------
ALTER TABLE users      ALTER COLUMN id RESTART WITH 100;
ALTER TABLE categories ALTER COLUMN id RESTART WITH 100;
ALTER TABLE requests   ALTER COLUMN id RESTART WITH 100;
