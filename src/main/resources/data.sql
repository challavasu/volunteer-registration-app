-- Fix NULL values in checked_in column for existing registrations
UPDATE registrations SET checked_in = false WHERE checked_in IS NULL;
