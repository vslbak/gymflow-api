-- Seed admin user
INSERT INTO gymflow_user (email, username, phone, password, role)
VALUES ('admin@gymflow.local','admin','1234567890',
'$2a$10$xjAan5repGpHU0/epnpTau2OZ4pvWPy1arvHIwOz4L4HlaVrqsOIa', -- << admin123
'ADMIN')
ON CONFLICT (email) DO NOTHING;

-- === Gymflow Classes Seed===
INSERT INTO gymflow_class
(id, name, instructor, duration, total_spots, image_url, category, level, location,
 description, price, what_to_bring, days, class_time)
VALUES
    ('00000000-0000-0000-0000-000000000001','Power Yoga Flow','Sarah Mitchell',INTERVAL '1 hour',20,
     'https://images.pexels.com/photos/3822166/pexels-photo-3822166.jpeg?auto=compress&cs=tinysrgb&w=800',
     'Yoga','Intermediate','Studio A, 2nd Floor',
     'An energizing yoga flow that builds strength, flexibility, and mindfulness. Perfect for intermediate practitioners looking to deepen their practice.',
     25.00,$$["Yoga mat","Water bottle","Comfortable athletic wear","Towel"]$$::jsonb,$$["MONDAY","WEDNESDAY"]$$::jsonb,'18:00'),

    ('00000000-0000-0000-0000-000000000002','HIIT Cardio Blast','Mike Johnson',INTERVAL '45 minutes',25,
     'https://images.pexels.com/photos/416809/pexels-photo-416809.jpeg?auto=compress&cs=tinysrgb&w=800',
     'Cardio','Advanced','Main Gym Floor',
     'High-intensity interval training designed to maximize calorie burn and boost your metabolism. Get ready to sweat!',
     30.00,$$["Water bottle","Towel","Athletic shoes","Heart rate monitor (optional)"]$$::jsonb,$$["TUESDAY","THURSDAY"]$$::jsonb,'07:30'),

    ('00000000-0000-0000-0000-000000000003','Strength Training','Alex Chen',INTERVAL '50 minutes',15,
     'https://images.pexels.com/photos/841130/pexels-photo-841130.jpeg?auto=compress&cs=tinysrgb&w=800',
     'Strength','All Levels','Weight Room',
     'Build lean muscle and increase your overall strength with progressive resistance training. Suitable for all fitness levels.',
     25.00,$$["Water bottle","Towel","Weight lifting gloves (optional)","Athletic shoes"]$$::jsonb,$$["MONDAY","FRIDAY"]$$::jsonb,'17:00'),

    ('00000000-0000-0000-0000-000000000004','Spin Class','Emma Davis',INTERVAL '45 minutes',30,
     'https://images.pexels.com/photos/3764011/pexels-photo-3764011.jpeg?auto=compress&cs=tinysrgb&w=800',
     'Cardio','Intermediate','Spin Studio',
     'An intense cycling workout set to energizing music. Push your limits and burn calories in this high-energy class.',
     28.00,$$["Water bottle","Towel","Cycling shoes or sneakers","Padded shorts (optional)"]$$::jsonb,$$["FRIDAY"]$$::jsonb,'08:00'),

    ('00000000-0000-0000-0000-000000000005','Pilates Core','Lisa Anderson',INTERVAL '55 minutes',18,
     'https://images.pexels.com/photos/3984340/pexels-photo-3984340.jpeg?auto=compress&cs=tinysrgb&w=800',
     'Pilates','Beginner','Studio B, 2nd Floor',
     'Focus on core strength, flexibility, and balance through controlled movements. Perfect for beginners and those recovering from injuries.',
     22.00,$$["Mat","Water bottle","Comfortable clothing","Small towel"]$$::jsonb,$$["TUESDAY","THURSDAY"]$$::jsonb,'09:00'),

    ('00000000-0000-0000-0000-000000000006','Boxing Fundamentals','James Wilson',INTERVAL '1 hour',20,
     'https://images.pexels.com/photos/4753928/pexels-photo-4753928.jpeg?auto=compress&cs=tinysrgb&w=800',
     'Boxing','All Levels','Boxing Ring',
     'Learn proper boxing techniques while getting an incredible full-body workout. Improve your coordination, speed, and power.',
     32.00,$$["Boxing gloves","Hand wraps","Water bottle","Athletic shoes","Mouthguard (optional)"]$$::jsonb,$$["MONDAY","WEDNESDAY","FRIDAY"]$$::jsonb,'19:00'),

    ('00000000-0000-0000-0000-000000000007','Morning Vinyasa','Sarah Mitchell',INTERVAL '1 hour',20,
     'https://images.pexels.com/photos/3822906/pexels-photo-3822906.jpeg?auto=compress&cs=tinysrgb&w=800',
     'Yoga','Beginner','Studio A, 2nd Floor',
     'Start your day with gentle flowing movements and mindful breathing. Perfect for all levels.',
     20.00,$$["Yoga mat","Water bottle","Comfortable clothing","Yoga blocks (optional)"]$$::jsonb,$$["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY"]$$::jsonb,'07:00'),

    ('00000000-0000-0000-0000-000000000008','Kettlebell Power','Alex Chen',INTERVAL '45 minutes',12,
     'https://images.pexels.com/photos/2261482/pexels-photo-2261482.jpeg?auto=compress&cs=tinysrgb&w=800',
     'Strength','Intermediate','Weight Room',
     'Dynamic kettlebell training to build explosive strength and power. Great for functional fitness.',
     28.00,$$["Water bottle","Towel","Athletic shoes","Workout gloves (optional)"]$$::jsonb,$$["TUESDAY","THURSDAY"]$$::jsonb,'18:00'),

    ('00000000-0000-0000-0000-000000000009','Restorative Yoga','Sarah Mitchell',INTERVAL '1 hour 15 minutes',15,
     'https://images.pexels.com/photos/3822220/pexels-photo-3822220.jpeg?auto=compress&cs=tinysrgb&w=800',
     'Yoga','Beginner','Studio A, 2nd Floor',
     'Relax and restore with gentle poses held for longer periods. Perfect for stress relief and recovery.',
     24.00,$$["Yoga mat","Blanket","Eye pillow (optional)","Comfortable loose clothing"]$$::jsonb,$$["SUNDAY"]$$::jsonb,'18:00'),

    ('00000000-0000-0000-0000-000000000010','Boot Camp Challenge','Mike Johnson',INTERVAL '1 hour',20,
     'https://images.pexels.com/photos/4164853/pexels-photo-4164853.jpeg?auto=compress&cs=tinysrgb&w=800',
     'Cardio','Advanced','Main Gym Floor',
     'Military-inspired workout combining cardio, strength, and agility drills. Prepare to be challenged!',
     35.00,$$["Water bottle","Towel","Athletic shoes","Energy snack","Heart rate monitor (optional)"]$$::jsonb,$$["SATURDAY"]$$::jsonb,'09:00'),

    ('00000000-0000-0000-0000-000000000011','Pilates Reformer','Lisa Anderson',INTERVAL '50 minutes',10,
     'https://images.pexels.com/photos/3775566/pexels-photo-3775566.jpeg?auto=compress&cs=tinysrgb&w=800',
     'Pilates','Intermediate','Studio B, 2nd Floor',
     'Advanced Pilates using the reformer machine for a challenging full-body workout.',
     30.00,$$["Water bottle","Grip socks","Comfortable fitted clothing","Small towel"]$$::jsonb,$$["MONDAY","WEDNESDAY"]$$::jsonb,'10:00'),

    ('00000000-0000-0000-0000-000000000012','Kickboxing Cardio','James Wilson',INTERVAL '55 minutes',25,
     'https://images.pexels.com/photos/4753994/pexels-photo-4753994.jpeg?auto=compress&cs=tinysrgb&w=800',
     'Boxing','Intermediate','Boxing Ring',
     'High-energy kickboxing workout combining martial arts techniques with cardio conditioning.',
     30.00,$$["Boxing gloves","Hand wraps","Water bottle","Athletic shoes","Towel"]$$::jsonb,$$["TUESDAY","THURSDAY"]$$::jsonb,'19:00'),

    ('00000000-0000-0000-0000-000000000013','CrossFit WOD','Alex Chen',INTERVAL '1 hour',15,
     'https://images.pexels.com/photos/1552106/pexels-photo-1552106.jpeg?auto=compress&cs=tinysrgb&w=800',
     'Strength','Advanced','Main Gym Floor',
     'Workout of the Day featuring Olympic lifts, gymnastics, and metabolic conditioning.',
     32.00,$$["Water bottle","Towel","Wrist wraps","Athletic shoes","Jump rope (optional)"]$$::jsonb,$$["MONDAY","WEDNESDAY","FRIDAY"]$$::jsonb,'17:30'),

    ('00000000-0000-0000-0000-000000000014','Zumba Dance Party','Emma Davis',INTERVAL '50 minutes',30,
     'https://images.pexels.com/photos/3775540/pexels-photo-3775540.jpeg?auto=compress&cs=tinysrgb&w=800',
     'Cardio','Beginner','Studio A, 2nd Floor',
     'Fun Latin-inspired dance fitness class that feels more like a party than a workout!',
     22.00,$$["Water bottle","Dance shoes or sneakers","Comfortable clothing","Positive energy"]$$::jsonb,$$["TUESDAY","THURSDAY"]$$::jsonb,'18:00'),

    ('00000000-0000-0000-0000-000000000015','Core & Abs Blast','Lisa Anderson',INTERVAL '30 minutes',20,
     'https://images.pexels.com/photos/3775164/pexels-photo-3775164.jpeg?auto=compress&cs=tinysrgb&w=800',
     'Pilates','All Levels','Studio B, 2nd Floor',
     'Intense 30-minute core workout targeting all abdominal muscles. Short but effective!',
     18.00,$$["Mat","Water bottle","Towel","Comfortable athletic wear"]$$::jsonb,$$["MONDAY","WEDNESDAY","FRIDAY"]$$::jsonb,'12:30');
